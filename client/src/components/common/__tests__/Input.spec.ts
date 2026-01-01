import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Input from '../Input.vue'

describe('Input', () => {
    describe('rendering', () => {
        it('should render input element', () => {
            const wrapper = mount(Input, {
                props: { modelValue: '' }
            })

            expect(wrapper.find('input').exists()).toBe(true)
        })

        it('should have input class', () => {
            const wrapper = mount(Input, {
                props: { modelValue: '' }
            })

            expect(wrapper.find('input').classes()).toContain('input')
        })
    })

    describe('v-model', () => {
        it('should display initial value', () => {
            const wrapper = mount(Input, {
                props: { modelValue: 'initial value' }
            })

            expect(wrapper.find('input').element.value).toBe('initial value')
        })

        it('should emit update:modelValue on input', async () => {
            const wrapper = mount(Input, {
                props: { modelValue: '' }
            })

            await wrapper.find('input').setValue('new value')

            expect(wrapper.emitted('update:modelValue')).toBeTruthy()
            expect(wrapper.emitted('update:modelValue')![0]).toEqual(['new value'])
        })
    })

    describe('props', () => {
        it('should apply placeholder', () => {
            const wrapper = mount(Input, {
                props: {
                    modelValue: '',
                    placeholder: 'Enter text...'
                }
            })

            expect(wrapper.find('input').attributes('placeholder')).toBe('Enter text...')
        })

        it('should apply disabled state', () => {
            const wrapper = mount(Input, {
                props: {
                    modelValue: '',
                    disabled: true
                }
            })

            expect(wrapper.find('input').attributes('disabled')).toBeDefined()
        })

        it('should apply type', () => {
            const wrapper = mount(Input, {
                props: {
                    modelValue: '',
                    type: 'password'
                }
            })

            expect(wrapper.find('input').attributes('type')).toBe('password')
        })
    })

    describe('error state', () => {
        it('should show error message', () => {
            const wrapper = mount(Input, {
                props: {
                    modelValue: '',
                    error: 'This field is required'
                }
            })

            expect(wrapper.find('.input-error-message').exists()).toBe(true)
            expect(wrapper.find('.input-error-message').text()).toBe('This field is required')
        })

        it('should apply error class', () => {
            const wrapper = mount(Input, {
                props: {
                    modelValue: '',
                    error: 'Error'
                }
            })

            expect(wrapper.find('input').classes()).toContain('input-error')
        })

        it('should not show error message when no error', () => {
            const wrapper = mount(Input, {
                props: { modelValue: '' }
            })

            expect(wrapper.find('.input-error-message').exists()).toBe(false)
        })
    })
})
